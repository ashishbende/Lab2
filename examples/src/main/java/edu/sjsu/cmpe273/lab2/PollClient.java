
/**
 * Created by ashish on 3/18/15.
 */

package edu.sjsu.cmpe273.lab2;

import io.grpc.ChannelImpl;
import io.grpc.transport.netty.NegotiationType;
import io.grpc.transport.netty.NettyChannelBuilder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PollClient {

    private static final Logger logger = Logger.getLogger(PollClient.class.getName());

    private final ChannelImpl channel;
    private final PollServiceGrpc.PollServiceBlockingStub blockingStub;

    public PollClient(String host, int port){
        logger.info("Contacting PollServer on ("+ host+") @port: " +port);
        channel=
                NettyChannelBuilder.forAddress(host, port).negotiationType(NegotiationType.PLAINTEXT)
                .build();
        blockingStub = PollServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException{
        channel.shutdown().awaitTerminated(5,TimeUnit.SECONDS);
    }


    public void poll(String moderatorId , String question, String startedAt, String expiredAt, String[] choice){
        if(choice == null || choice.length <2){
            new RuntimeException("Choice options not complete, 2 items needed.");
        }
        try{
            logger.info("Initiating new poll for Moderator " + moderatorId);
            PollRequest request = PollRequest.newBuilder()
                    .setModeratorId(moderatorId)
                    .setQuestion(question)
                    .setStartedAt(startedAt)
                    .setExpiredAt(expiredAt)
                    .addChoice(choice[0])
                    .addChoice(choice[1])
                    .build();
            PollResponse response = blockingStub.createPoll(request);
            logger.info("Initiated a new poll with id = " + response.getId());
        }catch(RuntimeException e){
            logger.log(Level.WARNING,"RPC Failed",e);
            return;
        }
    }


    public static void main(String[] args) throws Exception{
        PollClient client = new PollClient("ec2-54-153-99-214.us-west-1.compute.amazonaws.com",50051);
        try{
            /* update localhost or instance address according to server running */

            String moderatorId = "12345";
            String question = "What type of smartphone do you have?";
            String startedAt = "2015-03-18T13:00:00.000Z";
            String expiredAt = "2015-03-19T13:00:00.000Z";
            String[] choice = new String[] { "Android", "iPhone" };

            client.poll(moderatorId,question,startedAt,expiredAt,choice);

        }finally {
            client.shutdown();
        }
    }
}

